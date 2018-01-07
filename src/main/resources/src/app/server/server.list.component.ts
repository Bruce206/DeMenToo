import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import 'rxjs/add/operator/map';
import {Router} from "@angular/router";
import {ServerService} from "./server.service";


@Component({
  selector: 'server',
  templateUrl:'server.list.component.html',
  styleUrls: ['server.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ServerListComponent implements OnInit {

  private servers: any[] = [];

  constructor(private serversService: ServerService, private router: Router) {
  }

  ngOnInit(): void {
    this.serversService.getList().subscribe((data) => {
      this.servers = data;
    });
  }
}
