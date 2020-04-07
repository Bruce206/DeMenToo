import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {InstanceTypeComponent} from "./instancetype.component";
import {InstanceTypeInstancesComponent} from "./instancetype.instances.component";

const routes: Routes = [
  {
    path: '/:type/settings',
    component: InstanceTypeComponent,
    data: {title: 'Instanz-Typ Einstellungen'}
  },
  {
    path: '/:type/instances',
    component: InstanceTypeInstancesComponent,
    data: {title: 'Instanz-Typ Liste'}
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InstanceTypeRouting { }
