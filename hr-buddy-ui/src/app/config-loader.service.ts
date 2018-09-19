import { Injectable, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ConfigLoaderService {

  private appConfig: Config;

  constructor (private injector: Injector) { }

  loadAppConfig() {
    let http = this.injector.get(HttpClient);

    return http.get('/assets/app-config.json')
        .toPromise()
        .then(data => {
          this.appConfig = <any>data;
        })
  }

  get config(): Config {
    return this.appConfig;
  }
}

export interface Config {
  serverUrl: string
}
