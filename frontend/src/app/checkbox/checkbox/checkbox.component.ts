import {Component} from '@angular/core';
import {FormBuilder} from '@angular/forms';

/** @title Checkboxes with reactive forms */
@Component({
  selector: 'checkbox-continue',
  templateUrl: 'checkbox.component.html',
  styleUrls: ['checkbox.component.css'],
})
export class CheckboxComponent {
  actions = this._formBuilder.group({
    continue: false,
  });

  constructor(private _formBuilder: FormBuilder) {}
}
