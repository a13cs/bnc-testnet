import {Component, Inject} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {AppComponent} from "../app.component";


// export interface DialogData {
//   animal: string;
//   name: string;
// }

export interface PropsPair {
  key: string,
  value: string
}

/**
 * @title Dialog Overview
 */
@Component({
  selector: 'dialog-overview-example',
  templateUrl: 'dialog-overview-example.html',
})
export class DialogOverviewExample {

  static props: PropsPair[] = [
    {key: "api-key", value: "MqLTBCiXSVzyFVd70m31PSVPUOUeDerH304Tziy6AkbUtHbotiCBnJlqDTwDVQqg"},
    {key: "api-secret", value: "Yr5HgRB25lbhcJgBu8rvuQMaWlJQ6635800RMaFpAhdqE7g0TG1jj5AW9vva5VWS"},
    {key: "rest-uri", value: "https://testnet.binance.vision/api/v3/"},
    {key: "rest-uri-margin", value: "https://api.binance.com/sapi/v1/margin/"},
    {key: "position-entry", value: "0.02"},
    {key: "recv-window", value: "60000"},
    {key: "name", value: "BNC-1"},
    {key: "type", value: "SPOT"},
    {key: "isolated", value: "FALSE"}
  ]

  constructor(public dialog: MatDialog, private http: HttpClient) {}

  openDialog(): void {
    const dialogRef = this.dialog.open(DialogOverviewExampleDialog, {
      width: '500px',
      data: DialogOverviewExample.props
    });

    dialogRef.afterClosed().subscribe(result => {
//       console.log(result);
      let rez : PropsPair[] = result
      if(result) {
        let properties: any = {}
        rez.map(pair => properties[pair.key] = pair.value)

//         console.log(properties)

        this.saveProps(properties)
      }
    });
  }

  saveProps(props? : any) {
//     console.log(props)
    let h = new HttpHeaders()
    h.set('Content-Type', 'application/json')

    this.http.post('/saveProps', props, {headers: h}).subscribe( d => {
      console.log(d)
      AppComponent.accType = props['type']
    })

  }

}


@Component({
  selector: 'dialog-overview-example-dialog',
  templateUrl: 'dialog-overview-example-dialog.html',
})
export class DialogOverviewExampleDialog {

  constructor(
    public dialogRef: MatDialogRef<DialogOverviewExampleDialog>,
    @Inject(MAT_DIALOG_DATA) public data: PropsPair[]) {}

  onNoClick(): void {
    this.data = DialogOverviewExample.props
    this.dialogRef.close();
  }

  addProp() {
    const example = {key: "example-key", value: "example-value"}
    this.data.push(example)
  }

  deleteProp() {
    this.data.pop()
  }

}
