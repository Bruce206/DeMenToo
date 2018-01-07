import {Component} from '@angular/core';
import {MenuItem} from "primeng/primeng";

@Component({
  selector: 'app',
  templateUrl: 'frame.html'
})
export class AppComponent {

  items: MenuItem[];

  logout() {
    window.location.href = '/logout';
  }

  ngOnInit() {
    this.items = [
      {
        icon: 'fa-home',
        label: 'Home',
        routerLink: '/dashboard',
      },
      {
        label: 'Servers',
        icon: 'fa-file-o',
        routerLink: '/server'
      },
      {
        label: 'Apps',
        icon: 'fa-file-o',
        routerLink: '/instance'
      },
    ];
  }

}
