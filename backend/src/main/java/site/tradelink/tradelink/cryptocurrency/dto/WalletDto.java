package site.tradelink.tradelink.cryptocurrency.dto;

import site.tradelink.tradelink.cryptocurrency.entity.Wallet;

public record WalletDto(
        Long balance,
        Long availableBalance
) {
    public static WalletDto from(Wallet wallet) {
        return new WalletDto(wallet.getBalance(), wallet.getAvailableBalance());
    }
}
