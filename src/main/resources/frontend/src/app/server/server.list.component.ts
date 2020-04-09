import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {ServerService} from "./server.service";
import {Server} from "../java-types-module";

import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';

@Component({
  selector: 'server',
  templateUrl:'server.list.component.html',
  styleUrls: ['server.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ServerListComponent implements OnInit, OnDestroy {

  public servers: Server[] = [];
  public selectedServer: Server;
  private subscription: any;

  public ipResponses: any = {};
  public addedServer: any = {};
  public addServerDialogVisible: boolean = false;
  private connections: any = {};

  constructor(private serversService: ServerService) {
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.disconnect();
    }
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    delete this.selectedServer;
    this.serversService.getList().subscribe((data) => {
      this.servers = data;
    });
  }

  cleanServers() {
    this.serversService.cleanUp().subscribe(() => this.refresh());
  }

  blacklistServer(selectedServer: Server) {
    this.serversService.blacklist(selectedServer).subscribe(() => this.refresh());
  }

  deleteServer(selectedServer: Server) {
    this.serversService.delete(selectedServer).subscribe(() => this.refresh());
  }

  save(selectedServer: Server) {
    this.serversService.save(selectedServer).subscribe(() => this.refresh());
  }

  updateApacheConfs(selectedServer: any) {
    this.serversService.updateApacheConfs(selectedServer).subscribe(data => {
      selectedServer.apacheConfs = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
    });
  }

  pingApacheUrls(selectedServer: Server) {
    if (!this.subscription) {
      let ws = new SockJS("/socket");
      this.subscription = Stomp.over(ws);
      this.subscription.debug = true;
      let that = this;

      this.subscription.connect({}, function () {
        that.subscription.subscribe("/status/iptest", (data) => {
          let response: any = JSON.parse(data.body);
          that.ipResponses[response.url] = response;
        });
        that.serversService.pingApacheConfs(selectedServer).subscribe();
      });
    } else {
      this.serversService.pingApacheConfs(selectedServer).subscribe();
    }


  }

  addServer() {
    this.serversService.save(this.addedServer).subscribe(() => this.refresh());
    this.addedServer = {};
    this.addServerDialogVisible = false;
  }

  testConnection(server: Server) {
    this.serversService.testSSHConnection(server).subscribe(() => {
      this.refresh();
      alert("Connection successful!");
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.message);
    });
  }

  checkConnections() {
    for (let server of this.servers) {
      if (server.activeCheckDisabled) {
        this.connections[server.id] = "disabled";
      } else {
        this.serversService.testSSHConnection(server).subscribe(() => {
          this.connections[server.id] = "connected";
        }, error => {
          console.log(server.serverName);
          console.log(error);
          this.connections[server.id] = "no_connection";
        });
      }
    }
  }

  isConnectionTest() {
    return Object.keys(this.connections).length > 0;
  }

  clearSelectedServer() {
    delete this.selectedServer;
  }
}
