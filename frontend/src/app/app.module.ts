import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { HttpClientModule } from "@angular/common/http";
import {MatDatepickerModule} from '@angular/material/datepicker';

import { DialogOverviewExample, DialogOverviewExampleDialog } from './dialog/dialog.example.overview';
import { CanvasChartComponent } from './canvas/canvas-chart.component';


import { AppComponent } from './app.component';
import {DatePicker} from "./date-picker/date-picker.component";
import {MatNativeDateModule} from "@angular/material/core";
import {RouterModule} from "@angular/router";
import {MatIconModule} from "@angular/material/icon";
import { CheckboxComponent } from './checkbox/checkbox/checkbox.component';
import { SelectOptgroupComponent } from './dropdown/select-optgroup.component';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import { InfoComponent } from './info/info.component';
import { SimpleComponent } from './tabs/simple/simple.component';
import { FullComponent } from './tabs/full/full.component';
import { CollectComponent } from './tabs/collect/collect.component';

@NgModule({
  declarations: [
    AppComponent,
    DialogOverviewExample,
    DialogOverviewExampleDialog,
    CanvasChartComponent,
    DatePicker,
    CheckboxComponent,
    SelectOptgroupComponent,
    InfoComponent,
    SimpleComponent,
    FullComponent,
    CollectComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MatDialogModule,
    FormsModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    ReactiveFormsModule,
    RouterModule.forRoot([
      {path: 'welcome', component: CanvasChartComponent},
      {path: 'simple', component: CanvasChartComponent},
      {path: 'full', component: CanvasChartComponent},
      {path: '', redirectTo: 'welcome', pathMatch: 'full'},
      // {path: '**', component: PageNotFoundComponent}
    ]),
    MatIconModule,
    MatCheckboxModule,
    MatSelectModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
