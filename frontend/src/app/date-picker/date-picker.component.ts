import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";
import {Observable, Observer} from "rxjs";

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

  emitter: EventEmitter<any> = new EventEmitter()

  @Output() onDateChange: Observable<any> = Observable.create((o: Observer<any>)=> {
    this.emitter.subscribe(d => o.next(d))
  })

}
