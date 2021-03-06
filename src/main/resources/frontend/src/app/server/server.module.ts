import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {ServerListComponent} from "./server.list.component";
import {ServerRouting} from "./server.routing";
import {CardModule} from "primeng/card";
import {TableModule} from "primeng/table";
import {ButtonModule} from "primeng/button";
import {InstanceTableModule} from "../instance-table/instance-table.module";
import {AccordionModule} from "primeng/accordion";
import {DialogModule} from 'primeng/dialog';
import { ServerAnalysisComponent } from './server-analysis/server-analysis.component';


@NgModule({
  imports: [
    CommonModule, ServerRouting, UiComponentsModule, CardModule, TableModule, ButtonModule, InstanceTableModule, AccordionModule, DialogModule
  ],
  declarations: [
    ServerListComponent,
    ServerAnalysisComponent
  ],
  providers: []
})

export class ServerModule {

}

