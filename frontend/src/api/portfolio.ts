import client from './client'
import type { WalletDto, TotalAssetDto, HoldingDto } from '../types'

export const portfolioApi = {
  getWallet: () =>
    client.get<{ data: WalletDto }>('/auth/wallet').then((r) => r.data.data),

  getTotalAsset: () =>
    client.get<{ data: TotalAssetDto }>('/auth/wallet/total-asset').then((r) => r.data.data),

  deposit: (amount: number) =>
    client
      .post<{ data: WalletDto }>('/auth/wallet/deposit', null, { params: { amount } })
      .then((r) => r.data.data),

  getHoldings: () =>
    client.get<{ data: HoldingDto[] }>('/auth/holdings').then((r) => r.data.data),

  getHolding: (ticker: string) =>
    client.get<{ data: HoldingDto }>(`/auth/holdings/${ticker}`).then((r) => r.data.data),
}
