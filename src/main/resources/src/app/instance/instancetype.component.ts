import {Component, OnInit, ViewEncapsulation, OnDestroy} from '@angular/core';
import 'rxjs/add/operator/map';
import {ActivatedRoute} from "@angular/router";
import {SelectItem} from "primeng/primeng";
import {InstanceTypeService} from "./instancetype.service";
import {SortPipe} from "../sort.pipe";
import {StompService} from 'ng2-stomp-service';


@Component({
  selector: 'instancetype',
  templateUrl: 'instancetype.component.html',
  styleUrls: ['instance.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class InstanceTypeComponent implements OnInit, OnDestroy {
  public instanceType: any = {instances: []};
  public columnOptions: SelectItem[];
  public cols: any[];

  private subscription: any;

  constructor(private instanceTypeService: InstanceTypeService, private route: ActivatedRoute, private sortPipe: SortPipe, private stomp: StompService) {
  }

  private subscribeToInstanceHealth() {
    let stomp = this.stomp;
    let getUrl = window.location;
    let baseUrl = getUrl.protocol + "//" + getUrl.host;

    stomp.configure({
      host: baseUrl + '/socket',
      debug: false,
      queue: {'init': false}
    });


    stomp.startConnect().then(() => {
      stomp.done('init');
      this.subscription = stomp.subscribe('/instancestatus', this.updateInstanceStatus.bind(this));
    });
  }

  ngOnInit(): void {
    this.cols = [
      {field: 'prod', header: 'Prod', filter: false, pos: 1, class: "col-icon"},
      {field: 'identifier', header: 'App-Name', filter: true, pos: 2},
      {field: 'domains', header: 'Domain', filter: true, pos: 3},
      {field: 'licensedFor', header: 'Customer', filter: true, pos: 4},
      {field: 'version', header: 'Version', filter: true, pos: 5},
      {field: 'modified', header: 'Last Message', filter: false, pos: 6},
      {field: 'status', header: 'Status', filter: false, pos: 7}
    ];

    this.columnOptions = [];
    for (let i = 0; i < this.cols.length; i++) {
      this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
    }

    this.fetchInstanceType();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
    this.stomp.disconnect().then(() => console.log("Socket closd"));
  }

  fetchInstanceType() {
    this.columnOptions = [];
    for (let i = 0; i < this.cols.length; i++) {
      this.columnOptions.push({label: this.cols[i].header, value: this.cols[i]});
    }

    this.route.params.subscribe(params => {
      this.instanceTypeService.get(params['type']).subscribe((data) => {
        this.instanceType = data;
        this.subscribeToInstanceHealth();
        if (data.instances && data.instances.length > 0) {
          for (let instance of data.instances) {
            for (let detail of instance.details) {
              instance[detail.key] = instance.instanceDetailsByKey[detail.key][0].value;
            }
          }

          let pos = 8;
          this.sortPipe.transform(data.instances[0].details, 'key');
          for (let key of data.instanceDetailKeys) {
            this.columnOptions.push({label: key, value: {field: key, header: key, filter: true, pos: pos++}});
          }
        }
      });
    });
  }

  public updateInstanceStatus(data) {
    console.log(data);
    for (let i of this.instanceType.instances) {
      if (i.id === data.instance.id) {
        i.status = data.status;
        i.lastMessage = data.instance.lastMessage;
        i.lastMessageCritical = data.instance.lastMessageCritical;
        i.responseTime = data.responseTime;
      }
    }
  }

  lookupRowStyleClass(instance: any) {
    if (instance.status === undefined && instance.instanceType.healthUrl) {
      return "pending";
    }

    if (instance.lastMessageCritical || instance.status !== "OK") {
      if (instance.prod) {
        return "critical";
      } else {
        return "testcritical";
      }
    }
  }

  save() {
    this.instanceTypeService.save(this.instanceType).subscribe(it => {
      this.instanceType = it;
    });
  }

  onUploadFinished($event) {
    this.fetchInstanceType();
  }

  handleRowClick(event: any) {
    window.open('/instance/detail/' + event.data.id, "_blank");
  }

}
