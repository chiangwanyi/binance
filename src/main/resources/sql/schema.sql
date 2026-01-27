-- 新建 BTCUSDT 永续合约5m K线表
CREATE TABLE IF NOT EXISTS btcusdt_perpetual_5m_kline (
    open_time BIGINT PRIMARY KEY,
    open_price TEXT NOT NULL,
    high_price TEXT NOT NULL,
    low_price TEXT NOT NULL,
    close_price TEXT NOT NULL,
    volume TEXT NOT NULL,
    close_time BIGINT NOT NULL,
    quote_volume TEXT NOT NULL,
    trade_count INTEGER NOT NULL,
    taker_buy_volume TEXT NOT NULL,
    taker_buy_quote_volume TEXT NOT NULL
);

-- 新建 BTCUSDT 永续合约1h K线表
CREATE TABLE IF NOT EXISTS btcusdt_perpetual_1h_kline (
    open_time BIGINT PRIMARY KEY,
    open_price TEXT NOT NULL,
    high_price TEXT NOT NULL,
    low_price TEXT NOT NULL,
    close_price TEXT NOT NULL,
    volume TEXT NOT NULL,
    close_time BIGINT NOT NULL,
    quote_volume TEXT NOT NULL,
    trade_count INTEGER NOT NULL,
    taker_buy_volume TEXT NOT NULL,
    taker_buy_quote_volume TEXT NOT NULL
)