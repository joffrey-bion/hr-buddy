import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';

import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HrBuddyClient {

  constructor(private http: HttpClient) {
  }

  generateAgendas(planningFile: File): Promise<GenerateAgendasResponse> {
    const serverUrl = environment.serverUrl;
    const formData: FormData = new FormData();
    formData.append('planningFile', planningFile, planningFile.name);

    return this.http.post<GenerateAgendasResponse>(`${serverUrl}/planning`, formData, {}).toPromise()
  }
}

export interface GenerateAgendasResponse {
  downloadUrl?: string,
  error?: string,
}
