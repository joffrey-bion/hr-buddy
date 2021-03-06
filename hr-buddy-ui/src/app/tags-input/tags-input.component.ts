import {Component, Input, ViewChild} from '@angular/core';

@Component({
  selector: 'app-tags-input',
  templateUrl: './tags-input.component.html',
  styleUrls: ['./tags-input.component.css'],
})
export class TagsInputComponent {

  @ViewChild('input') inputEl;

  @Input()
  items = [];

  @Input()
  placeholder: string;

  constructor() {
  }

  add(item: string) {
    if (item.length > 0) {
      this.items.push(item);
    }
    this.clearInput();
  }

  remove(item: string) {
    this.items.splice(this.items.indexOf(item), 1);
    this.inputEl.nativeElement.focus();
  }

  clearInput() {
    this.inputEl.nativeElement.value = '';
  }
}
