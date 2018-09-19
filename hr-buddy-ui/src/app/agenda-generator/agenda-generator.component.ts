import {Component} from '@angular/core';
import {GenerateAgendasResponse, HrBuddyClient} from '../hr-buddy-client.service';

@Component({
  selector: 'app-agenda-generator',
  templateUrl: './agenda-generator.component.html',
  styleUrls: ['./agenda-generator.component.css'],
})
export class AgendaGeneratorComponent {

  planningFile?: File = null;

  loading: boolean = false;

  downloadUrl?: string = null;

  error?: string = null;

  constructor(private hrBuddyClient: HrBuddyClient) {
  }

  handleFileInput(files: File[]) {
    this.planningFile = files[0]
  }

  onSubmit() {
    this.loading = true;
    this.error = null;
    this.downloadUrl = null;

    return this.hrBuddyClient.generateAgendas(this.planningFile)
        .then((data: GenerateAgendasResponse) => {
          this.error = data.error;
          this.downloadUrl = data.downloadUrl;
        })
        .catch((e) => this.error = e.message)
        .then(() => this.loading = false);
  }
}
