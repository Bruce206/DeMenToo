/**
 * Created by bruss on 27.02.2017.
 */
import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable()
export class InstanceService {
  constructor(private http: HttpClient) {
  }

  get(id: number): Observable<any> {
    return this.http.get("/api/instance/" + id);
  }

  getList(): Observable<any> {
    return this.http.get<Array<any>>("/api/instance");
  }

  save(instance: any): Observable<any> {
    return this.http.put("/api/instance", instance);
  }

  delete(instance: any) {
    return this.http.delete("/api/instance/" + instance.id);
  }

  refreshHealth() {
    this.http.get("/api/instance/refresh").subscribe();
  }

  refreshHealthForType(type: string) {
    this.http.get("/api/instance/refresh/" + type).subscribe();
  }

}
