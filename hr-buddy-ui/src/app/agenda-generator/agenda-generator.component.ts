import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-agenda-generator',
  templateUrl: './agenda-generator.component.html',
  styleUrls: ['./agenda-generator.component.css'],
})
export class AgendaGeneratorComponent implements OnInit {

  planningFile: File | null = null;

  error: Error | null = null;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  handleFileInput(files: File[]) {
    this.planningFile = files[0]
  }

  onSubmit() {
    this.error = null;

    const endpoint = 'http://localhost:8080/planning';
    const formData: FormData = new FormData();
    formData.append('planningFile', this.planningFile, this.planningFile.name);
    return this.http.post(endpoint, formData, {}).toPromise()
        .then(() => console.log('hey!'))
        .catch((e) => this.error = e);
  }

}
