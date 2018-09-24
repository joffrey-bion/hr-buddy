import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';

import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HrBuddyClient {

  constructor(private http: HttpClient) {
  }

  generateAgendas(planningFile: File, options: GenerationOptions): Promise<GenerateAgendasResponse> {
    const serverUrl = environment.serverUrl;

    const formData: FormData = new FormData();
    formData.append('planningFile', planningFile, planningFile.name);
    formData.append('options', new Blob([JSON.stringify(options)], {
      type: "application/json"
    }));

    return this.http.post<GenerateAgendasResponse>(`${serverUrl}/planning`, formData, {}).toPromise()
  }
}

export interface GenerateAgendasResponse {
  downloadUrl?: string,
  error?: string,
}

export interface GenerationOptions {
  jobTitlesWithNoDivision?: string[],
}
