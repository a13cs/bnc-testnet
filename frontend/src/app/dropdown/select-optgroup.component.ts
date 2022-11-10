import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Observable} from "rxjs";

interface Interval {
  value: string;
  viewValue: string;
}

interface IntervalGroup {
  disabled?: boolean;
  name: string;
  interval: Interval[];
}

@Component({
  selector: 'select-interval',
  templateUrl: 'select-optgroup.component.html'
})
export class SelectOptgroupComponent {
  @Input()
  selected: string = '';

  emitter: EventEmitter<string> = new EventEmitter<string>()

  @Output() onSelect: Observable<string> = Observable.create((observer: { next: (arg0: string) => void; error: (arg0: string) => any; }) => {
   this.emitter.subscribe(d => observer.next(d))
  })

  intervalGroups: IntervalGroup[] = [
    {
      name: 'sec',
      interval: [
        {value: '1s', viewValue: '1'},
        {value: '3s', viewValue: '3'},
        {value: '5s', viewValue: '5'},
        {value: '15s', viewValue: '15'},
        {value: '10s', viewValue: '10'},
        {value: '30s', viewValue: '30'},
        {value: '45s', viewValue: '45'},
      ],
    },
    {
      name: 'min',
      interval: [
        {value: '1m', viewValue: '1'},
        {value: '3m', viewValue: '3'},
        {value: '5m', viewValue: '5'},
        {value: '15m', viewValue: '15'},
        {value: '30m', viewValue: '30'},
        {value: '45m', viewValue: '45'},
      ],
    },
    {
      name: 'hours',
      // disabled: true,
      interval: [
        {value: '1h', viewValue: '1'},
        {value: '2h', viewValue: '2'},
        {value: '4h', viewValue: '4'},
        {value: '8h', viewValue: '8'},
      ],
    },
    {
      name: 'days',
      interval: [
        {value: '1D', viewValue: '1'},
      ],
    },
    {
      name: 'weeks',
      interval: [
        {value: '1W', viewValue: '1'},
      ],
    },
    {
      name: 'months',
      interval: [
        {value: '1M', viewValue: '1'},
      ],
    },
  ];

  onChange() {
    this.emitter.emit(this.selected)
    console.log(this.selected)

  }
}
