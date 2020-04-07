import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export class HasPermission {
  target: string;
  minLevel: number;

  constructor(target: string, minLevel: number) {
    this.target = target;
    this.minLevel = minLevel;
  }
}

@Injectable()
export class CurrentUserService /*implements Resolve<any>*/ {
  public user: any;
  public device: any;

  constructor(private http: HttpClient) {

  }

  init(): Promise<any> {
    return this.http.get<any>('/currentUser').toPromise().then(data => {
      this.user = data.principal;
      this.device = data.device;
    });
  }

  hasRole(role: string): boolean {
    return true; // this.user.authorities.filter(authority => authority.authority === role).length != 0;
  }

  hasPermission(permission: HasPermission): boolean {
    if (this.user.principal.authorities.find(item => item.authority === 'ROLE_ADMIN')) {
      return true;
    }

    console.log(permission.target);

    const userLevel = this.user.principal.permissions.find(item => item.target === permission.target);

    if (userLevel === undefined) {
      return false;
    }

    return userLevel.level >= permission.minLevel;
  }

  getProperty(key: string) {
    if (this.user.principal.user.properties) {
      return this.user.principal.user.properties[key];
    }
  }
}
