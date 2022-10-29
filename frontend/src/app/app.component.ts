import {Component, OnInit } from '@angular/core';
// import {createChart, CrosshairMode, ISeriesApi, UTCTimestamp} from 'lightweight-charts';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {FormControl, FormGroup} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {InfoComponent} from "./info/info.component";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'app';
  jarLocation = 'localhost:8080'

  data: any[] = []
  series: any = []

// indicators: any[]
//   fastEma: any = []
//   shortEma: any = []

  static isolated: string = "FALSE"
  get isAccIsolated() {
    return AppComponent.isolated
  }

  static accType: string = "SPOT"
  get accTypeValue() {
    return AppComponent.accType
  }

  balances: Balance[] | { asset: any; free: any; }[] = [];

  myTrades: any [] = [];
  showTrades: boolean = false

  proxyConf = false
  prefix = this.proxyConf ? '/be' : ''

  chart : any = {}

  example : string = ""

  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  constructor(private http: HttpClient, public dialog: MatDialog) {
  }

  openDialog() {
    this.dialog.open(InfoComponent);
  }


  buy(value? : any) {
    console.log(value)
    this.http.get<any[]>(this.prefix + '/order/BUY/BTCUSDT/' + value ).subscribe( d => console.log(d) )
  }

  sell(value? : any) {
    console.log(value)
    this.http.get<any[]>(this.prefix + '/order/SELL/BTCUSDT/' + value ).subscribe( d => console.log(d) )
  }

  ngOnInit(): void {
  // todo: preserve indent
    this.example =
`
      //@version=5
      strategy("EMA Strategy", overlay=true)
      fastLength = input(30)
      slowLength = input(80)

      slowEma = ta.ema(close, slowLength)
      fastEma = ta.ema(close, fastLength)

      if (ta.crossover(fastEma, slowEma))
        strategy.entry("EmaLE", strategy.long, comment="EmaLE")
      if (ta.crossunder(fastEma, slowEma))
        strategy.entry("EmaSE", strategy.short, comment="EmaSE")

      plot(slowEma, title="slow", color=color.blue, linewidth=1, style=plot.style_line)
      plot(fastEma, title="fast", color=color.red, linewidth=2, style=plot.style_line)
`
//
//     const chart = createChart(document.body  /* this.context.canvas */, {
//       width: 900,
//       height: 500,
//       timeScale: {
//         // barSpacing: 4,
//         timeVisible: true,
//         secondsVisible: true,
//       },
//       crosshair: {
//         mode: CrosshairMode.Normal,
//       }
//     });
// //     console.log(chart)
//
//     this.chart = chart;
//
//     chart.applyOptions({
//       watermark: {
//         color: 'rgba(11, 94, 29, 0.4)',
//         visible: true,
//         text: 'Positions',
//         fontSize: 24,
//         horzAlign: 'left',
//         vertAlign: 'bottom',
//       },
//     });
//
//     this.series = chart.addCandlestickSeries();

    // ================================================

//     this.fastEma = chart.addLineSeries({
//       color: 'rgb(10,22,125)',
//       lineWidth: 3,
//     });
//
//     this.shortEma = chart.addLineSeries({
//       color: 'rgb(4,107,232)',
//       lineWidth: 2,
//     });

    // chart.addHistogramSeries()

    // let volumeSeries = chart.addHistogramSeries({
    //   color: '#26a69a',
    //   priceFormat: {
    //     type: 'volume',
    //   },
    //   priceScaleId: '',
    //   scaleMargins: {
    //     top: 0.8,
    //     bottom: 0,
    //   },
    // });
    // {time, value, color}


//     const chartLine = createChart(document.body, {
//       width: 600,
//       height: 300,
//       layout: {
//         backgroundColor: '#ffffff',
//         textColor: 'rgba(33, 56, 77, 1)',
//       },
//       grid: {
//         vertLines: {
//           color: 'rgba(197, 203, 206, 0.7)',
//         },
//         horzLines: {
//           color: 'rgba(197, 203, 206, 0.7)',
//         },
//       },
//       timeScale: {
//         timeVisible: true,
//         secondsVisible: true,
//         // fixLeftEdge: true
//       },
//     });
//
//     chartLine.applyOptions({
//       watermark: {
//         color: 'rgba(2,28,6,0.4)',
//         visible: true,
//         text: 'Close Price Line Chart',
//         fontSize: 24,
//         horzAlign: 'left',
//         vertAlign: 'bottom',
//       },
//     });
//
//     // TODO: add trade quantity dots graph
//     this.closeLine = chartLine.addLineSeries({
//       color: 'rgb(17,10,151)',
//       lineWidth: 2,
//     });

    // ================================================

//     console.log("accType " + AppComponent.accType)
//     this.http.get<AccType>(this.prefix + '/accType').subscribe( d => {
//       console.log("accType type " + d.type)
//       AppComponent.accType = d.type
//       AppComponent.isolated = d.isolated
//       console.log("accType response " + AppComponent.accType)
//     })
//
//     if(AppComponent.accType === "SPOT") {
//       this.http.get<Acc>(this.prefix + '/acc').subscribe( d => {
//         console.log(d)
//         this.balances = d.balances?.filter(b => b.asset == 'BTC' || b.asset == 'USDT') || []
//       })
//     } else if (AppComponent.accType === "MARGIN") {
//       this.http.get<MarginAcc>(this.prefix + '/acc').subscribe( d => {
//         console.log(d)
//         if (AppComponent.isolated === "TRUE") {
//           this.balances = d.assets?.filter(b => b.symbol == 'BTCUSDT').flatMap(a => {
//             return [
//             {asset : a.baseAsset.asset, free : a.baseAsset.free},
//             {asset : a.quoteAsset.asset, free : a.quoteAsset.free}
//             ]
//           })
//         } else {
//           this.balances = d.userAssets?.filter(b => b.asset == 'BTC' || b.asset == 'USDT') || []
//         }
//       })
//     } else this.balances = []
//
//
//
// //   + use date-picker
//     let start = new Date(Date.UTC(2022, 8, 22, 0, 0, 0, 0)).getTime() // 1000
// //     let end = new Date(Date.UTC(2022, 12, 1, 0, 0, 0, 0)).getTime() // 1000
//     let end = new Date().getTime() // 1000
//     console.log("start: " + start)
//     console.log("end: " + end)
//
//     let interval = '1m'
//     this.http.get<any[]>(this.prefix + '/klines/' + /* start */ 0 + '/' + /* end */ 0 + '/' + interval).subscribe(
//       d => {
//         let lineData: any[] = []
//
//         let csvTimestamp = d.slice(0,1)[0][0];
//         console.log("csvTimestamp: " + csvTimestamp)
//
//         d.slice(0).forEach( point => {
// //           console.log(+point[0])
//           if(+point[0]) {
//               this.data.push({
//                 open: point[/*"openPrice"*/1] | 0,
//                 high: point[/*"highPrice"*/2] | 0,
//                 low: point[/*"lowPrice"*/3] | 0,
//                 close: point[/*"closePrice"*/4] | 0,
//                 time: +point[/*"startTime"*/0] /1000 as UTCTimestamp
//               })
//               // let p : number = (Math.round(point[4] * 1000) / 1000)//.toFixed(2);
//               lineData.push({time: +point[0]/1000 as UTCTimestamp, value: point[4] | 0})
//             }
//         })
//         this.series.setData(this.data);
//
//         this.http.get<any[]>(this.prefix + '/myTrades').subscribe( d => {
//           this.myTrades = d
//
//           let signals: any[] = d.map(s => {
//             return {
//               time:  +s['time'] /1000 as UTCTimestamp,
//               color: s['isBuyer'] === true ? 'rgb(7,130,19)' : 'rgb(113,10,11)',
//               position: 'aboveBar',
//               shape: s['isBuyer'] === true ? "arrowUp" : "arrowDown",
//               text: (s['quoteQty']) || ''
//             }
//           })
//           // + use separate chart
//           this.series.setMarkers(signals)
//         })
//       })

  }

  getAcc() {
    console.log("accType " + AppComponent.accType)

    if(AppComponent.accType == "SPOT") {
      this.http.get<Acc>(this.prefix + '/acc').subscribe( d => {
        console.log(d)
        this.balances = d.balances?.filter(b => b.asset === 'BTC' || b.asset === 'USDT') || []
      })
    } else if (AppComponent.accType == "MARGIN") {
      this.http.get<MarginAcc>(this.prefix + '/acc').subscribe( d => {
        console.log(d)
        if (AppComponent.isolated === "TRUE") {
          this.balances = d.assets?.filter(b => b.symbol == 'BTCUSDT').flatMap(a => {
            return [
            {asset : a.baseAsset.asset, free : a.baseAsset.free},
            {asset : a.quoteAsset.asset, free : a.quoteAsset.free}
            ]
          })
        } else {
          this.balances = d.userAssets?.filter(b => b.asset === 'BTC' || b.asset === 'USDT') || []
        }
      })
    } else this.balances = []
  }

  refresh() {
//   let a : HTMLElement = document.getElementsByClassName('a')[0] as HTMLElement
//       const chart = createChart(  a, {
//         width: 900,
//         height: 500,
//         timeScale: {
//           // barSpacing: 4,
//           timeVisible: true,
//           secondsVisible: true,
//         },
//         crosshair: {
//           mode: CrosshairMode.Normal,
//         }
//       });
//     document.getElementsByClassName('a')[0].innerHTML = document.getElementsByClassName('tv-lightweight-charts')[0].innerHTML
//     console.log(document.getElementsByClassName('tv-lightweight-charts'))

//     this.ngOnInit()
  }

  test(value?: string) {
      // add test signals
//       let h = new HttpHeaders()
//       h.set('Content-Type', 'application/json')
//
//       let p : any = {script: value, type: 'pine'} // jython
//       this.http.post<Result>(this.prefix + '/test/0/0/1m', p, {headers: h}).subscribe( d => {
// //         console.log(d)
// //       })
// //
// //       this.http.get<Result>(this.prefix + '/test/0/0/1m').subscribe( d => {
//
//         let sig: any[] = d.signals.map(s => {
//           return {
//             time:  +s[0] /1000 as UTCTimestamp,
//             color: s[1] === 'B' ? 'rgb(7,130,19)' : 'rgb(113,10,11)',
//             position: 'belowBar',
//             shape: s[1] === 'B' ? "arrowUp" : "arrowDown",
//             text: (s[1]) || ''
//           }
//         })
//         this.series.setMarkers(sig)
//
//       // add indicators: fastEma, slowEma
//         d.indicators.forEach(i => {
//           console.log(i.name)
//
//           let lineSeries = this.chart.addLineSeries({
//             color: i.color, // 'rgb(4,107,232)',
//             lineWidth: 2,
//           });
//
//           let lineData: any[] = i.values.map(ema => {
//             return {
//               time:  +ema[0] /1000 as UTCTimestamp,
//               value: ema[1] || 0
//             }
//           })
//
//           lineSeries.setData(lineData)
//         })
//       })

  }

  save(value?: string) {
    //  save version
    console.log(value)
  }

  logRange() {
    console.log(this.range.controls.start.value)
  }
}

  export interface Acc {
    balances: Balance[]
  }

  export interface MarginAcc {
    userAssets: Balance[],
    assets: BalanceIsolated[]
  }

  export interface BalanceIsolated {
    symbol: string,
    baseAsset: any,
    quoteAsset: any
  }

  export interface AccType {
    type: string,
    isolated: string
  }

  export interface Balance {
    asset: string,
    free: string,
  //   locked: string
  }

  export interface Result {
    signals: any[],
    indicators: Indicator[],
  }

  export interface Indicator {
    name: string,
    type: string,
    color: string,
    values: any[]
  }
