/**
 * Created by bruss on 27.02.2017.
 */
import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {CombinedDomainContainer, Server} from "../java-types-module";
import {serializeI18nMessageForGetMsg} from "@angular/compiler/src/render3/view/i18n/get_msg_utils";

@Injectable({
  providedIn: 'root'
})
export class ServerService {
  constructor(private http: HttpClient) {
  }

  get(id: number): Observable<Server> {
    return this.http.get("/api/server/" + id);
  }

  getList(): Observable<any> {
    return this.http.get<Array<Server>>("/api/server");
  }

  save(server: Server): Observable<any> {
    return this.http.post("/api/server", server);
  }

  delete(server: any) {
    return this.http.delete("/api/server/" + server.id);
  }

  cleanUp(): Observable<any> {
    return this.http.post("/api/server/clean-up", {});
  }

  blacklist(server: Server) {
    return this.http.post("/api/server/blacklist/" + server.id, {});
  }

  updateApacheConfs(server: Server) {
    return this.http.get("/api/server/check-apache-confs/" + server.id);
  }

  pingUrls(server: Server, urlType: string) {
    return this.http.get("/api/server/ping-" + urlType + "/" + server.id);
  }

  testSSHConnection(server: Server) {
    return this.http.get("/api/server/test-ssh-connection/" + server.id);
  }

  updateXibisOneDomains(server: Server) {
    return this.http.get("/api/server/check-xibisone-domains/" + server.id);
  }

  updateCombinedDomainContainers(server: Server): Observable<CombinedDomainContainer[]> {
    return this.http.get<CombinedDomainContainer[]>("/api/server/check-combined-domains/" + server.id);
  }

  updateCombinedDomainContainersForAll() {
    return this.http.get<CombinedDomainContainer[]>("/api/server/check-combined-domains");
  }

  getCombinedDomainContainersForAll() {
    return this.http.get<CombinedDomainContainer[]>("/api/server/get-combined-domains");
  }
}
