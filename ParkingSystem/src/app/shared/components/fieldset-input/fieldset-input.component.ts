import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-fieldset-input',
  templateUrl: './fieldset-input.component.html',
  styleUrls: ['./fieldset-input.component.css']
})
export class FieldsetInputComponent {
  @Input() public label: string = '';
}
