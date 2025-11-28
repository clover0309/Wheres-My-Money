import axios from "axios";
import { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext";

function StockDetailModal({ isOpen, onClose, stock }) {
	const { user } = useAuth();
	const [stockDetailList, setStockDetailList] = useState([]);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState(null);

	useEffect(function() {
		if (isOpen && stock) {
			loadStockDetail();
		}
	}, [isOpen, stock]);

	const loadStockDetail = async function() {
		setIsLoading(true);
		setError(null);

		try {
			const response = await axios.get(
				`http://localhost:8080/api/stock/detail?userId=${user}&stockCode=${stock.userstockStk}`
			);

			if (response.data.success) {
				// 백엔드에서 List<StockDetailDTO>를 반환
				const dataList = Array.isArray(response.data.data) ? response.data.data : [response.data.data];
				setStockDetailList(dataList);
			} else {
				setError(response.data.message || '상세 정보를 불러오는데 실패했습니다.');
			}
		} catch (err) {
			console.error('주식 상세 정보 조회 오류:', err);
			setError('주식 상세 정보를 불러오는 중 오류가 발생했습니다.');
		} finally {
			setIsLoading(false);
		}
	};

	const getProfitColor = function(profitRate) {
		if (profitRate > 0) return '#dc3545'; // 빨간색 (수익)
		if (profitRate < 0) return '#0066cc'; // 파란색 (손실)
		return '#000000'; // 검은색 (보합)
	};

	const formatNumber = function(num) {
		return Math.floor(Number(num)).toLocaleString();
	};

	if (!isOpen) return null;

	return (
		<div style={{
			position: 'fixed',
			top: 0,
			left: 0,
			right: 0,
			bottom: 0,
			backgroundColor: 'rgba(0, 0, 0, 0.5)',
			display: 'flex',
			justifyContent: 'center',
			alignItems: 'center',
			zIndex: 1000
		}}>
			<div style={{
				backgroundColor: 'white',
				borderRadius: '8px',
				padding: '30px',
				maxWidth: '600px',
				width: '90%',
				maxHeight: '80vh',
				overflowY: 'auto',
				boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
			}}>
				<div style={{ 
					display: 'flex', 
					justifyContent: 'space-between', 
					alignItems: 'center',
					marginBottom: '20px',
					borderBottom: '2px solid #007bff',
					paddingBottom: '10px'
				}}>
					<h2 style={{ margin: 0, color: '#007bff' }}>주식 상세 정보</h2>
					<button
						onClick={onClose}
						style={{
							backgroundColor: 'transparent',
							border: 'none',
							fontSize: '24px',
							cursor: 'pointer',
							color: '#666'
						}}
					>
						×
					</button>
				</div>

				{isLoading ? (
					<div style={{ textAlign: 'center', padding: '40px 0' }}>
						<p>로딩 중...</p>
					</div>
				) : error ? (
					<div style={{ 
						textAlign: 'center', 
						padding: '40px 0',
						color: '#dc3545'
					}}>
						<p>{error}</p>
						<button
							onClick={loadStockDetail}
							style={{
								marginTop: '10px',
								padding: '8px 16px',
								backgroundColor: '#007bff',
								color: 'white',
								border: 'none',
								borderRadius: '4px',
								cursor: 'pointer'
							}}
						>
							다시 시도
						</button>
					</div>
				) : stockDetailList && stockDetailList.length > 0 ? (
					<div>
						{/* 종목 기본 정보 */}
						<div style={{ 
							backgroundColor: '#f8f9fa', 
							padding: '20px', 
							borderRadius: '8px',
							marginBottom: '20px'
						}}>
							<h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
								{stockDetailList[0].stockName}
							</h3>
							<div style={{ fontSize: '14px', color: '#666' }}>
								<p style={{ margin: '5px 0' }}>종목코드: <strong>{stockDetailList[0].stockCode}</strong></p>
								<p style={{ margin: '5px 0' }}>현재가: <strong>{formatNumber(stockDetailList[0].currentPrice)}원</strong></p>
							</div>
						</div>

						{/* 날짜별 매수내역 테이블 */}
						<div style={{ marginBottom: '20px' }}>
							<h4 style={{ marginBottom: '10px', color: '#333' }}>매수 내역</h4>
							<table style={{ 
								width: '100%', 
								borderCollapse: 'collapse',
								border: '1px solid #dee2e6',
								fontSize: '14px'
							}}>
								<thead>
									<tr style={{ backgroundColor: '#007bff', color: 'white' }}>
										<th style={{ padding: '12px', border: '1px solid #dee2e6' }}>매수일</th>
										<th style={{ padding: '12px', border: '1px solid #dee2e6' }}>수량</th>
										<th style={{ padding: '12px', border: '1px solid #dee2e6' }}>평균단가</th>
										<th style={{ padding: '12px', border: '1px solid #dee2e6' }}>평가손익</th>
										<th style={{ padding: '12px', border: '1px solid #dee2e6' }}>수익률</th>
									</tr>
								</thead>
								<tbody>
									{stockDetailList.map(function(detail, index) {
										return (
											<tr key={index} style={{ backgroundColor: index % 2 === 0 ? '#fff' : '#f8f9fa' }}>
												<td style={{ 
													padding: '10px', 
													border: '1px solid #dee2e6',
													textAlign: 'center'
												}}>
													{detail.purchaseDate}
												</td>
												<td style={{ 
													padding: '10px', 
													border: '1px solid #dee2e6',
													textAlign: 'right'
												}}>
													{formatNumber(detail.quantity)}주
												</td>
												<td style={{ 
													padding: '10px', 
													border: '1px solid #dee2e6',
													textAlign: 'right'
												}}>
													{formatNumber(detail.averagePrice)}원
												</td>
												<td style={{ 
													padding: '10px', 
													border: '1px solid #dee2e6',
													textAlign: 'right',
													color: getProfitColor(detail.profitLoss),
													fontWeight: 'bold'
												}}>
													{detail.profitLoss > 0 ? '+' : ''}{formatNumber(detail.profitLoss)}원
												</td>
												<td style={{ 
													padding: '10px', 
													border: '1px solid #dee2e6',
													textAlign: 'right',
													color: getProfitColor(detail.profitRate),
													fontWeight: 'bold'
												}}>
													{detail.profitRate > 0 ? '+' : ''}{detail.profitRate.toFixed(2)}%
												</td>
											</tr>
										);
									})}
									{/* 합계 행 */}
									<tr style={{ backgroundColor: '#fff3cd', fontWeight: 'bold' }}>
										<td style={{ 
											padding: '12px', 
											border: '1px solid #dee2e6',
											textAlign: 'center'
										}}>
											합계
										</td>
										<td style={{ 
											padding: '12px', 
											border: '1px solid #dee2e6',
											textAlign: 'right'
										}}>
											{formatNumber(stockDetailList.reduce(function(sum, detail) {
												return sum + detail.quantity;
											}, 0))}주
										</td>
										<td style={{ 
											padding: '12px', 
											border: '1px solid #dee2e6',
											textAlign: 'right'
										}}>
											-
										</td>
										<td style={{ 
											padding: '12px', 
											border: '1px solid #dee2e6',
											textAlign: 'right',
											color: getProfitColor(stockDetailList.reduce(function(sum, detail) {
												return sum + Number(detail.profitLoss);
											}, 0)),
											fontWeight: 'bold'
										}}>
											{(function() {
												const totalProfitLoss = stockDetailList.reduce(function(sum, detail) {
													return sum + Number(detail.profitLoss);
												}, 0);
												return (totalProfitLoss > 0 ? '+' : '') + formatNumber(totalProfitLoss) + '원';
											})()}
										</td>
										<td style={{ 
											padding: '12px', 
											border: '1px solid #dee2e6',
											textAlign: 'right',
											color: getProfitColor((function() {
												const totalInvestment = stockDetailList.reduce(function(sum, detail) {
													return sum + (Number(detail.averagePrice) * detail.quantity);
												}, 0);
												const totalEvaluation = stockDetailList.reduce(function(sum, detail) {
													return sum + Number(detail.evaluationAmount);
												}, 0);
												return ((totalEvaluation - totalInvestment) / totalInvestment) * 100;
											})()),
											fontWeight: 'bold'
										}}>
											{(function() {
												const totalInvestment = stockDetailList.reduce(function(sum, detail) {
													return sum + (Number(detail.averagePrice) * detail.quantity);
												}, 0);
												const totalEvaluation = stockDetailList.reduce(function(sum, detail) {
													return sum + Number(detail.evaluationAmount);
												}, 0);
												const totalProfitRate = ((totalEvaluation - totalInvestment) / totalInvestment) * 100;
												return (totalProfitRate > 0 ? '+' : '') + totalProfitRate.toFixed(2) + '%';
											})()}
										</td>
									</tr>
								</tbody>
							</table>
						</div>

						{/* 닫기 버튼 */}
						<div style={{ textAlign: 'center', marginTop: '20px' }}>
							<button
								onClick={onClose}
								style={{
									padding: '10px 30px',
									backgroundColor: '#6c757d',
									color: 'white',
									border: 'none',
									borderRadius: '4px',
									cursor: 'pointer',
									fontSize: '16px'
								}}
								onMouseOver={function(e) { e.target.style.backgroundColor = '#5a6268'; }}
								onMouseOut={function(e) { e.target.style.backgroundColor = '#6c757d'; }}
							>
								닫기
							</button>
						</div>
					</div>
				) : null}
			</div>
		</div>
	);
}

export default StockDetailModal;
