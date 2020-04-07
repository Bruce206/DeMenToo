import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ServerListComponent} from "./server.list.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: ServerListComponent,
    data: {title: 'Server'}
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServerRouting { }
