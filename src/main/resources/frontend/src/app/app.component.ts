import {Component} from '@angular/core';
import {MenuItem} from "primeng/api";
import {InstanceTypeService} from "./instancetype/instancetype.service";

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
        icon: 'fas fa-home',
        label: 'Home',
        routerLink: '/dashboard',
      },
      {
        label: 'Server',
        icon: 'fas fa-server',
        routerLink: '/server'
      },
      {
        label: '------ Analysis',
        routerLink: '/server/analysis'
      },
      {
        label: 'Apps',
        icon: 'fas fa-cubes',
        routerLink: '/instance'
      },
      {
        separator: true
      }
    ];

    this.instanceTypeService.getList().subscribe(types => {
      for (let type of types) {
        this.items.push({icon: 'fas fa-cubes', label: type.name});
        this.items.push({icon: 'fas fa-cogs', label: "Settings", routerLink: '/instanceType/' + type.name + "/settings", styleClass: "indentMenuItem"});
        this.items.push({icon: 'fas fa-list', label: "Instances", routerLink: '/instanceType/' + type.name + "/instances", styleClass: "indentMenuItem"});
      }
    });
  }

}
