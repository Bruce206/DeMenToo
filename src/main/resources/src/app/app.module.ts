import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {AppComponent} from './app.component';
import {DashboardComponent} from './dashboard/dashboard.component'
import {
  MenuModule, ButtonModule, PanelModule, SharedModule, DataTableModule, AccordionModule, FieldsetModule,
  DropdownModule, GalleriaModule, SidebarModule, CheckboxModule, TieredMenuModule, FileUploadModule, DataGridModule,
  DialogModule, LightboxModule, MenubarModule, MultiSelectModule, OverlayPanelModule
} from "primeng/primeng";
import {CalendarModule} from 'primeng/calendar';
import {CardModule} from 'primeng/card';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";
import {ServerListComponent} from "./server/server.list.component";
import {ServerService} from "./server/server.service";
import {InstanceListComponent} from "./instance/instance.list.component";
import {InstanceService} from "./instance/instance.service";
import {SortPipe} from "./sort.pipe";
import {InstanceDetailComponent, KeysPipe} from "./instance/instance.detail.component";
import {FormRowComponent} from "./uiComponents/formrow/form-row.component";
import {InstanceTypeService} from "./instancetype/instancetype.service";
import {InstanceTypeComponent} from "./instancetype/instancetype.component";
import { StompService } from 'ng2-stomp-service';
import {InstanceTypeInstancesComponent} from "./instancetype/instancetype.instances.component";

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    TieredMenuModule, MenuModule, ButtonModule, PanelModule, DataTableModule, SharedModule,
    DropdownModule, GalleriaModule, AccordionModule, FieldsetModule, SidebarModule, CheckboxModule,
    FileUploadModule, DataGridModule, DialogModule, LightboxModule, MenubarModule, MultiSelectModule,
    OverlayPanelModule, CardModule, CalendarModule,
    RouterModule.forRoot([
      {
        path: '',
        redirectTo: '/instance',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        component: DashboardComponent
      },
      {
        path: 'server',
        component: ServerListComponent
      },
      {
        path: 'instance',
        component: InstanceListComponent
      },
      {
        path: 'instance/detail/:id',
        component: InstanceDetailComponent
      },
      {
        path: 'instanceType/:type/settings',
        component: InstanceTypeComponent
      },
      {
        path: 'instanceType/:type/instances',
        component: InstanceTypeInstancesComponent
      }
    ])

  ],
  declarations: [
    FormRowComponent,
    AppComponent,
    DashboardComponent,
    ServerListComponent,
    InstanceListComponent,
    InstanceDetailComponent,
    InstanceTypeComponent,
    InstanceTypeInstancesComponent,
    SortPipe,
    KeysPipe
  ],
  providers: [
    ServerService,
    InstanceService,
    InstanceTypeService,
    SortPipe,
    StompService
  ],
  entryComponents: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}

