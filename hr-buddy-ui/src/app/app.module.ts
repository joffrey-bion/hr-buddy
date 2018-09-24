import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome'
import {library} from '@fortawesome/fontawesome-svg-core';
import {
  faBug,
  faCheckCircle,
  faClock,
  faCogs,
  faDownload,
  faExclamationCircle,
  faFileExcel,
  faQuestionCircle,
  faUpload,
} from '@fortawesome/free-solid-svg-icons';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AgendaGeneratorComponent} from './agenda-generator/agenda-generator.component';
import {AppComponent} from './app.component';
import {TagsInputComponent} from './tags-input/tags-input.component';

library.add(faBug);
library.add(faCheckCircle);
library.add(faClock);
library.add(faCogs);
library.add(faDownload);
library.add(faExclamationCircle);
library.add(faFileExcel);
library.add(faQuestionCircle);
library.add(faUpload);

@NgModule({
  declarations: [
    AppComponent,
    AgendaGeneratorComponent,
    TagsInputComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    NgbModule,
    FontAwesomeModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {
}
