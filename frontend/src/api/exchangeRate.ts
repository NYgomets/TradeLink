import client from './client'
import type { ExchangeRateSummaryDto, ExchangeRateChartPointDto, Period } from '../types'

export const exchangeRateApi = {
  getLatest: () =>
    client.get<{ data: ExchangeRateSummaryDto[] }>('/exchange-rates').then((r) => r.data.data),

  getTable: (currencyCode: string, period: Period) =>
    client
      .get<{ data: ExchangeRateSummaryDto[] }>(`/exchange-rates/${currencyCode}/table`, {
        params: { period },
      })
      .then((r) => r.data.data),

  getChart: (currencyCode: string, period: Period) =>
    client
      .get<{ data: ExchangeRateChartPointDto[] }>(`/exchange-rates/${currencyCode}/chart`, {
        params: { period },
      })
      .then((r) => r.data.data),
}
