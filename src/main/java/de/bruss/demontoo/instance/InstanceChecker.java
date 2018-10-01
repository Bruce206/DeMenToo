package de.bruss.demontoo.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class InstanceChecker {

	private final Logger logger = LoggerFactory.getLogger(InstanceChecker.class);

	@Autowired
	private InstanceTypeService instanceTypeService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${unreachable-instance-mail}")
	private String mailto;

	@Value("${unreachable-instance-enable-mail}")
	private boolean unreachableInstanceEnableMail;

	private LocalDateTime lastPassiveMailSent;
	private LocalDateTime lastActiveMailSent;

	private HashMap<Instance, LocalDateTime> repeatedNonCallingInstances = new HashMap<>();

	private enum CheckType {
		ACTIVE, PASSIVE
	}

//	@Scheduled(cron = "10 * * * * *")
//	public void passiveCheck() {
//		if (unreachableInstanceEnableMail) {
//			// only send mail once per half hour
//			if (lastPassiveMailSent == null || lastPassiveMailSent.plusMinutes(10).isBefore(LocalDateTime.now())) {
//				List<InstanceType> instanceTypes = instanceTypeService.findAll();
//
//				List<Instance> nonCallingInstances = new ArrayList<>();
//
//				for (InstanceType it : instanceTypes) {
//					for (Instance i : it.getInstances()) {
//						if (!i.isExcludeFromHealthcheck() && i.getLastMessage().plusMinutes(it.getMessageInterval()).isBefore(LocalDateTime.now())) {
//							if (!repeatedNonCallingInstances.containsKey(i)) {
//								repeatedNonCallingInstances.put(i, LocalDateTime.now());
//								nonCallingInstances.add(i);
//							}
//						} else {
//							repeatedNonCallingInstances.remove(i);
//						}
//					}
//				}
//
//				if (!nonCallingInstances.isEmpty()) {
//					sendMail(nonCallingInstances, CheckType.PASSIVE);
//				}
//			}
//		}
//	}

//	@Scheduled(cron = "0 0 * * * *")
//	public void sendRepeatedNonCallingInstancesMail() {
//		logger.error("Instances down: " + repeatedNonCallingInstances.size());
//		MimeMessage mail = javaMailSender.createMimeMessage();
//		try {
//			MimeMessageHelper helper = new MimeMessageHelper(mail, false);
//			helper.setTo(mailto.split(","));
//			helper.setReplyTo("info@eins-gmbh.de");
//			helper.setFrom("demontoo@eins-gmbh.de");
//			helper.setPriority(1);
//			helper.setSubject("Seiten länger nicht erreichbar");
//
//
//			StringBuilder sb = new StringBuilder();
//			sb.append("Folgende Seiten sind nicht mehr erreichbar:\n\n");
//			sb.append("<table><thead><th>Instance</th><th>Url</th><th>Server</th><th>Last Message</th></thead><tbody>");
//			for (Instance i : repeatedNonCallingInstances.keySet()) {
//				sb.append("<tr>");
//				sb.append("<td>");
//				sb.append(i.getIdentifier());
//				sb.append("</td>");
//				sb.append("<td>");
//				if (!i.getDomains().isEmpty()) {
//					sb.append(i.getDomains().get(0).getUrl());
//				}
//				sb.append("</td>");
//				sb.append("<td>");
//				sb.append(i.getServer().getServerName());
//				sb.append("</td>");
//				sb.append("<td>");
//				sb.append(i.getLastMessage());
//				sb.append("</td>");
//
//				sb.append("</tr>");
//			}
//
//			helper.setText(sb.toString(), true);
//		} catch (MessagingException e) {
//			logger.error("Could not send mail", e);
//		}
//
//		javaMailSender.send(mail);
//	}

	@Scheduled(cron = "10 */15 * * * *")
	public void activeCheck() {
		if (unreachableInstanceEnableMail) {
			// only send mail once per 10 mins
			if (lastActiveMailSent == null || lastActiveMailSent.plusMinutes(10).isBefore(LocalDateTime.now())) {
				List<InstanceType> instanceTypes = instanceTypeService.findAll();

				List<Instance> unreachableInstances = new ArrayList<>();

				for (InstanceType it : instanceTypes) {
					for (Instance instance : it.getInstances()) {
						if (!instance.isExcludeFromHealthcheck() && !instance.getDomains().isEmpty() && instance.getDomains().get(0) != null && !StringUtils.isEmpty(instance.getDomains().get(0).getUrl()) && instance.getInstanceType().getHealthUrl() != null) {
							int timeout = 10000;
							HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
							clientHttpRequestFactory.setConnectTimeout(timeout);
							clientHttpRequestFactory.setReadTimeout(timeout);

							RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

							String instanceUrl = instance.getDomains().get(0).getUrl();
							if (!instanceUrl.startsWith("http")) {
								instanceUrl = "http://" + instanceUrl;
							}

							logger.debug("checking health for: " + instanceUrl + instance.getInstanceType().getHealthUrl());
							try {
								int tries = 0;
								HttpStatus status = HttpStatus.MOVED_PERMANENTLY;
								String url = instanceUrl + instance.getInstanceType().getHealthUrl();
								while (HttpStatus.MOVED_PERMANENTLY.equals(status) && tries++ < 10) {
									ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
									status = response.getStatusCode();
									if (HttpStatus.MOVED_PERMANENTLY.equals(status)) {
										url = response.getHeaders().getLocation().toString();
									} else {
										if (HttpStatus.OK.equals(status)) {
											instance.setLastMessage(LocalDateTime.now());
										} else {
											unreachableInstances.add(instance);
										}
									}
								}

								if (tries == 10) {
									unreachableInstances.add(instance);
								}
							} catch (Exception e) {
								unreachableInstances.add(instance);
							}
						}
					}
				}

				if (!unreachableInstances.isEmpty()) {
					sendMail(unreachableInstances, CheckType.ACTIVE);
				}
			}
		}
	}

	private void sendMail(List<Instance> unreachableInstances, CheckType checkType) {
		String subject;
		if (checkType.equals(CheckType.ACTIVE)) {
			lastActiveMailSent = LocalDateTime.now();
			subject = unreachableInstances.size() + " Seiten nicht erreichbar!!";
		} else {
			lastPassiveMailSent = LocalDateTime.now();
			subject = unreachableInstances.size() + " Seiten haben sich zu lange nicht gemeldet!!";
		}

		logger.error("Instances down: " + unreachableInstances.size());
		MimeMessage mail = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mail, false);
			helper.setTo(mailto.split(","));
			helper.setReplyTo("info@eins-gmbh.de");
			helper.setFrom("demontoo@eins-gmbh.de");
			helper.setPriority(1);
			helper.setSubject(subject);

			StringBuilder sb = new StringBuilder();
			sb.append("Folgende Seiten sind nicht mehr erreichbar:\n\n");
			for (Instance i : unreachableInstances) {
				sb.append(i.getIdentifier());
				if (!i.getDomains().isEmpty()) {
					sb.append(" [");
					sb.append(i.getDomains().get(0).getUrl());
					sb.append("] ");
				}
				sb.append(" auf Server: ");
				sb.append(i.getServer().getServerName());
				sb.append(". Letzte Meldung: ");
				sb.append(i.getLastMessage()).append("\n");
			}

			helper.setText(sb.toString());
		} catch (MessagingException e) {
			logger.error("Could not send mail", e);
		}

		javaMailSender.send(mail);
	}

}
