/**
 * Created by bruss on 27.02.2017.
 */
import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable()
export class InstanceTypeService {
  constructor(private http: HttpClient) {
  }

  get(id: number): Observable<any> {
    return this.http.get("/api/instanceType/" + id);
  }

  getList(): Observable<any> {
    return this.http.get<Array<any>>("/api/instanceType");
  }

  save(instanceType: any): Observable<any> {
    return this.http.post("/api/instanceType", instanceType);
  }

  delete(instanceType: any) {
    return this.http.delete("/api/instanceType/" + instanceType.id);
  }

}
