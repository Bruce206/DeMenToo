import { Component, OnInit } from '@angular/core';
import {ServerService} from "../server.service";
import {CombinedDomainContainer} from "../../java-types-module";

@Component({
  selector: 'app-server-analysis',
  templateUrl: './server-analysis.component.html',
  styleUrls: ['./server-analysis.component.scss']
})
export class ServerAnalysisComponent implements OnInit {
  private combinedDomains: CombinedDomainContainer[];
  public updating: boolean = false;

  constructor(private serverService: ServerService) { }

  ngOnInit(): void {
    this.serverService.getCombinedDomainContainersForAll().subscribe(data => {
      this.combinedDomains = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
    });
  }

  updateCombinedDomainContainers() {
    this.updating = true;
    this.serverService.updateCombinedDomainContainersForAll().subscribe(data => {
      this.updating = false;
      this.combinedDomains = data;
    }, error => {
      console.error(error);
      alert("Connection could not be established: " + error.error.message);
      this.updating = false;
    });
  }

  getCombinedColor(cdc: CombinedDomainContainer) {
    return (cdc.inApache && cdc.inXibisOne && cdc.pingStatus === "SAME_SERVER") ? '' : '#ff9898';
  }
}
