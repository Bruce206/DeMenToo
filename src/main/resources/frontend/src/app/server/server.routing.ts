import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {ServerListComponent} from "./server.list.component";
import {ServerAnalysisComponent} from "./server-analysis/server-analysis.component";

const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    component: ServerListComponent,
    data: {title: 'Server'}
  },
  {
    path: 'analysis',
    component: ServerAnalysisComponent,
    data: {title: 'Server-Analysis'}
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServerRouting { }
