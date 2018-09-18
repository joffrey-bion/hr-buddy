import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AgendaGeneratorComponent } from './agenda-generator.component';

describe('AgendaGeneratorComponent', () => {
  let component: AgendaGeneratorComponent;
  let fixture: ComponentFixture<AgendaGeneratorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgendaGeneratorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgendaGeneratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
