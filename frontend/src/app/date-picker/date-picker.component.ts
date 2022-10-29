import {Component, Input} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";

/** @title Basic date range picker */
@Component({
  selector: 'date-picker',
  templateUrl: './date-picker.component.html'
})
export class DatePicker {
  @Input()
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
}
