import {NgModule} from '@angular/core';
import {CommonModule} from "@angular/common";
import {UiComponentsModule} from "../core/uiComponents/uiComponents.module";
import {ServerListComponent} from "./server.list.component";
import {ServerRouting} from "./server.routing";

@NgModule({
    imports: [
      CommonModule, ServerRouting, UiComponentsModule,
    ],
  declarations: [
    ServerListComponent
  ],
  providers: []
})

export class ServerModule {

}

