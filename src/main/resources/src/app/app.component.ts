import {Component} from '@angular/core';
import {MenuItem} from "primeng/primeng";
import {InstanceTypeService} from "./instance/instancetype.service";

@Component({
  selector: 'app',
  templateUrl: 'frame.html'
})
export class AppComponent {

  constructor(private instanceTypeService: InstanceTypeService) {

  }

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
        label: 'Server',
        icon: 'fa-server',
        routerLink: '/server'
      },
      {
        label: 'Apps',
        icon: 'fa-cubes',
        routerLink: '/instance'
      },
      {
        separator: true
      }
    ];

    this.instanceTypeService.getList().subscribe(types => {
      for (let type of types) {
        this.items.push({icon: 'fa-cubes', label: type.name, routerLink: '/instanceType/' + type.name});
      }
    });
  }

}
