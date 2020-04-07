package de.bruss.demontoo.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
public class SecurityController {

	@RequestMapping(value = "/currentUser", method = RequestMethod.GET)
	@ResponseBody
	public PrincipalDeviceContainer getCurrentUser(Principal principal) {
		return new PrincipalDeviceContainer(principal);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class PrincipalDeviceContainer  {
		private Principal principal;
	}
}
