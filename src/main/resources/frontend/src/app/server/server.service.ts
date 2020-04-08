/**
 * Created by bruss on 27.02.2017.
 */
import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {Server} from "../java-types-module";
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
}
