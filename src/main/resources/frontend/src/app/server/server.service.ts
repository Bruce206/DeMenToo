/**
 * Created by bruss on 27.02.2017.
 */
import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ServerService {
  constructor(private http: HttpClient) {
  }

  get(id: number): Observable<any> {
    return this.http.get("/api/server/" + id);
  }

  getList(): Observable<any> {
    return this.http.get<Array<any>>("/api/server");
  }

  save(template: any): Observable<any> {
    return this.http.post("/api/server", template);
  }

  delete(template: any) {
    return this.http.delete("/api/server/" + template.id);
  }

}
