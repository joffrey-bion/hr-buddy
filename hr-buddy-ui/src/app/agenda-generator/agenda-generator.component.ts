import {Component, ElementRef, ViewChild} from '@angular/core';
import {environment} from '../../environments/environment';
import {GenerateAgendasResponse, HrBuddyClient} from '../hr-buddy-client.service';
import {TagsInputComponent} from '../tags-input/tags-input.component';

@Component({
  selector: 'app-agenda-generator',
  templateUrl: './agenda-generator.component.html',
  styleUrls: ['./agenda-generator.component.css'],
})
export class AgendaGeneratorComponent {

  examplePlanningUrl: string = `${environment.serverUrl}/example-planning.xlsx`;

  planningFile?: File = null;

  specialJobTitles: string[] = ["HR Consultant", "Recruitment Specialist"];

  loading: boolean = false;

  downloadUrl?: string = null;

  error?: string = null;

  @ViewChild('fileInput')
  fileInput: ElementRef;

  @ViewChild('tagsInput')
  tagsInput: TagsInputComponent;

  constructor(private hrBuddyClient: HrBuddyClient) {
  }

  handleFileInput(files: File[]) {
    this.error = null;
    this.downloadUrl = null;
    this.planningFile = files[0];
  }

  onSubmit() {
    this.loading = true;

    const options = {
      jobTitlesWithNoDivision: this.specialJobTitles
    };

    return this.hrBuddyClient.generateAgendas(this.planningFile, options)
        .then((data: GenerateAgendasResponse) => {
          this.error = AgendaGeneratorComponent.formatErrorMsg(data.error);
          this.downloadUrl = data.downloadUrl;
        })
        .catch((e) => this.error = `There was a problem contacting the server: ${e.message}`)
        .then(() => this.loading = false);
  }

  private static formatErrorMsg(error?: string): string {
    if (!error) {
      return null;
    }
    return `Something is incorrect in the provided excel file:\n${error}`
  }

  resetForm() {
    this.planningFile = null;
    this.specialJobTitles = ["HR Consultant", "Recruitment Specialist"];
    this.error = null;
    this.downloadUrl = null;
    this.loading = false;
    // the only way of clearing the actual file input (https://stackoverflow.com/a/40165524/1540818)
    this.fileInput.nativeElement.value = "";
    this.tagsInput.clearInput();
  }
}
