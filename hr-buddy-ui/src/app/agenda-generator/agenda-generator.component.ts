import {HttpClient} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-agenda-generator',
  templateUrl: './agenda-generator.component.html',
  styleUrls: ['./agenda-generator.component.css'],
})
export class AgendaGeneratorComponent implements OnInit {

  planningFile?: File = null;

  loading: boolean = false;

  downloadUrl?: string = null;

  error?: string = null;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  handleFileInput(files: File[]) {
    this.planningFile = files[0]
  }

  onSubmit() {
    this.loading = true;
    this.error = null;
    this.downloadUrl = null;

    const endpoint = 'http://localhost:8080/planning';
    const formData: FormData = new FormData();
    formData.append('planningFile', this.planningFile, this.planningFile.name);

    return this.http.post<SendPlanningResponse>(endpoint, formData, {}).toPromise()
        .then((data) => {
          this.error = data.error;
          this.downloadUrl = data.downloadUrl;
        })
        .catch((e) => this.error = e.message)
        .then(() => this.loading = false);
  }

}

interface SendPlanningResponse {
  downloadUrl?: string,
  error?: string,
}
