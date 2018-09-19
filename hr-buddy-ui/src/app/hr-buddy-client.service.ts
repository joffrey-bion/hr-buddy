import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ConfigLoaderService} from './config-loader.service';

@Injectable({
  providedIn: 'root'
})
export class HrBuddyClient {

  private readonly serverUrl: string;

  constructor(private http: HttpClient, private environment: ConfigLoaderService) {
    this.serverUrl = this.environment.config.serverUrl;
  }

  generateAgendas(planningFile: File): Promise<GenerateAgendasResponse> {
    // const serverUrl = this.environment.config.serverUrl;
    const endpoint = `${this.serverUrl}/planning`;
    const formData: FormData = new FormData();
    formData.append('planningFile', planningFile, planningFile.name);

    return this.http.post<GenerateAgendasResponse>(endpoint, formData, {}).toPromise()
  }
}

export interface GenerateAgendasResponse {
  downloadUrl?: string,
  error?: string,
}
