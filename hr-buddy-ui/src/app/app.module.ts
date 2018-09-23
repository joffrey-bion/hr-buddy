import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {library} from '@fortawesome/fontawesome-svg-core';
import {
  faBug,
  faCheckCircle,
  faClock,
  faCogs,
  faDownload,
  faExclamationCircle,
  faFileExcel,
  faUpload,
} from '@fortawesome/free-solid-svg-icons';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AgendaGeneratorComponent} from './agenda-generator/agenda-generator.component';
import {AppComponent} from './app.component';

library.add(faBug);
library.add(faCheckCircle);
library.add(faClock);
library.add(faCogs);
library.add(faDownload);
library.add(faExclamationCircle);
library.add(faFileExcel);
library.add(faUpload);

@NgModule({
  declarations: [
    AppComponent,
    AgendaGeneratorComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    NgbModule,
    FontAwesomeModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {
}
