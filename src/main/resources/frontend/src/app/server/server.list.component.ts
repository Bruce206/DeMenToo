import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {ServerService} from "./server.service";
import {CombinedDomainContainer, Server} from "../java-types-module";

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

  constructor(private serverService: ServerService) {
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
    this.serverService.getList().subscribe((data) => {
      this.servers = data;
    });
  }

  cleanServers() {
    this.serverService.cleanUp().subscribe(() => this.refresh());
  }

  blacklistServer(selectedServer: Server) {
    this.serverService.blacklist(selectedServer).subscribe(() => this.refresh());
  }

  deleteServer(selectedServer: Server) {
    this.serverService.delete(selectedServer).subscribe(() => this.refresh());
  }

  save(selectedServer: Server) {
    this.serverService.save(selectedServer).subscribe(() => this.refresh());
  }

  updateApacheConfs(selectedServer: any) {
    this.serverService.updateApacheConfs(selectedServer).subscribe(data => {
      selectedServer.apacheConfs = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
    });
  }

  updateXibisOneDomains(selectedServer: any) {
    this.serverService.updateXibisOneDomains(selectedServer).subscribe(data => {
      selectedServer.xibisOneDomains = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
    });
  }

  pingUrls(selectedServer: Server, urlType: string) {
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
        that.serverService.pingUrls(selectedServer, urlType).subscribe();
      });
    } else {
      this.serverService.pingUrls(selectedServer, urlType).subscribe();
    }
  }

  addServer() {
    this.serverService.save(this.addedServer).subscribe(() => this.refresh());
    this.addedServer = {};
    this.addServerDialogVisible = false;
  }

  testConnection(server: Server) {
    this.serverService.testSSHConnection(server).subscribe(() => {
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
        this.serverService.testSSHConnection(server).subscribe(() => {
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


  updateCombinedDomainContainers(selectedServer: Server) {
    this.serverService.updateCombinedDomainContainers(selectedServer).subscribe(data => {
      selectedServer.combinedDomains = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
    });
  }

  getCombinedColor(cdc: CombinedDomainContainer) {
    return (cdc.inApache && cdc.inXibisOne && cdc.pingStatus === "SAME_SERVER") ? '' : '#ff9898';
  }
}
