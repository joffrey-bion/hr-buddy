import {HttpClientModule} from '@angular/common/http';
import {NgModule, APP_INITIALIZER} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {AgendaGeneratorComponent} from './agenda-generator/agenda-generator.component';
import {AppComponent} from './app.component';
import {ConfigLoaderService} from './config-loader.service';

const appInitializerFn = (appConfig: ConfigLoaderService) => {
  return () => {
    return appConfig.loadAppConfig();
  }
};

@NgModule({
  declarations: [
    AppComponent,
    AgendaGeneratorComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
  ],
  providers: [
    ConfigLoaderService,
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFn,
      multi: true,
      deps: [ConfigLoaderService]
    }
  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}
