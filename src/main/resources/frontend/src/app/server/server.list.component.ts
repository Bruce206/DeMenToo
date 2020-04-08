import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {ServerService} from "./server.service";
import {Server} from "../java-types-module";


@Component({
  selector: 'server',
  templateUrl:'server.list.component.html',
  styleUrls: ['server.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ServerListComponent implements OnInit {

  public servers: Server[] = [];
  public selectedServer: Server;

  constructor(private serversService: ServerService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.serversService.getList().subscribe((data) => {
      this.servers = data;
    });
  }

  cleanServers() {
    this.serversService.cleanUp().subscribe(() => {
      this.refresh();
    });
  }

  blacklistServer(selectedServer: Server) {
    this.serversService.blacklist(selectedServer).subscribe(() => {
      delete this.selectedServer;
      this.refresh();
    });
  }

  deleteServer(selectedServer: Server) {
    this.serversService.delete(selectedServer).subscribe(() => {
      delete this.selectedServer;
      this.refresh();
    });
  }

  save(selectedServer: Server) {
    this.serversService.save(selectedServer).subscribe(() => {
      delete this.selectedServer;
      this.refresh();
    });
  }
}
