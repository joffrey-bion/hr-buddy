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

  error?: Error = null;

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

    const endpoint = 'http://localhost:8080/planning';
    const formData: FormData = new FormData();
    formData.append('planningFile', this.planningFile, this.planningFile.name);

    return this.http.post(endpoint, formData, {
      responseType: 'arraybuffer',
    }).toPromise()
        .then(() => console.log('hey!'))
        .catch((e) => this.error = e)
        .then(() => this.loading = false);
  }

}
