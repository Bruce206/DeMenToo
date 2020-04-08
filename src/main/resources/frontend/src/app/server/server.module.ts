import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {ServerListComponent} from "./server.list.component";
import {ServerRouting} from "./server.routing";
import {CardModule} from "primeng/card";
import {TableModule} from "primeng/table";
import {ButtonModule} from "primeng/button";
import {InstanceTableModule} from "../instance-table/instanceTable.module";
import {AccordionModule} from "primeng/accordion";

@NgModule({
  imports: [
    CommonModule, ServerRouting, UiComponentsModule, CardModule, TableModule, ButtonModule, InstanceTableModule, AccordionModule
  ],
  declarations: [
    ServerListComponent
  ],
  providers: []
})

export class ServerModule {

}

