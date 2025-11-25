import axios from "axios";
import { useState } from "react";
import { useAuth } from "../contexts/AuthContext";

function AddStockModal({ isOpen, onClose, onSuccess }) {
	const { user } = useAuth();
	const [keyword, setKeyword] = useState('');
	const [searchResults, setSearchResults] = useState([]);
	const [selectedStock, setSelectedStock] = useState(null);
	const [quantity, setQuantity] = useState('');
	const [averagePrice, setAveragePrice] = useState('');
	const [purchaseDate, setPurchaseDate] = useState('');
	const [useAutoPrice, setUseAutoPrice] = useState(false);
	const [isSearching, setIsSearching] = useState(false);
	const [isSubmitting, setIsSubmitting] = useState(false);

	const handleSearch = async () => {
		if (keyword.trim().length < 1) {
			alert('검색어를 입력해주세요.');
			return;
		}

		setIsSearching(true);
		try {
			const response = await axios.get(`http://localhost:8080/api/stock/search?keyword=${keyword}`);
			setSearchResults(response.data);
		} catch (error) {
			console.error('검색 오류:', error);
			alert('검색에 실패했습니다.');
		} finally {
			setIsSearching(false);
		}
	};

	const handleSelectStock = (stock) => {
		setSelectedStock(stock);
		setSearchResults([]);
		setKeyword('');
	};

	const handleSubmit = async () => {
		console.log('handleSubmit 호출됨');
		
		if (!selectedStock) {
			alert('종목을 선택해주세요.');
			return;
		}

		if (!quantity || parseInt(quantity) <= 0) {
			alert('수량을 입력해주세요.');
			return;
		}

		// 자동 가격 조회를 사용할 경우 날짜 필수
		if (useAutoPrice) {
			if (!purchaseDate) {
				alert('매수 날짜를 입력해주세요.');
				return;
			}
		} else {
			// 수동 입력 시 평균 매수가 필수
			if (!averagePrice || parseFloat(averagePrice) <= 0) {
				alert('평균 매수가를 입력해주세요.');
				return;
			}
		}

		setIsSubmitting(true);
		
		try {
			const requestData = {
				userId: user,
				stockCode: selectedStock.code,
				stockName: selectedStock.name,
				quantity: parseInt(quantity)
			};

			// 자동 가격 조회 사용 시 날짜 전송, 아니면 수동 입력 가격 전송
			if (useAutoPrice) {
				requestData.purchaseDate = purchaseDate;
				requestData.averagePrice = null;
			} else {
				requestData.averagePrice = parseFloat(averagePrice);
				requestData.purchaseDate = null;
			}

			console.log('요청 데이터:', requestData);

			const response = await axios.post('http://localhost:8080/api/stock/add', requestData, {
				headers: {
					'Content-Type': 'application/json'
				}
			});

			console.log('서버 응답:', response.data);

			if (response.data.success) {
				alert('주식이 추가되었습니다.');
				resetForm();
				onSuccess();
				onClose();
			} else {
				const errorMsg = response.data.message || '주식 추가에 실패했습니다.';
				if (useAutoPrice) {
					alert(errorMsg + '\n\n자동 가격 조회에 실패했습니다.\n체크박스를 해제하고 평균 매수가를 직접 입력해주세요.');
				} else {
					alert(errorMsg);
				}
			}
		} catch (error) {
			console.error('주식 추가 오류:', error);
			console.error('에러 응답:', error.response?.data);
			
			let errorMessage = '주식 추가에 실패했습니다.';
			if (useAutoPrice) {
				errorMessage += '\n\n자동 가격 조회가 실패했습니다.\nAPI 연결 문제가 있을 수 있습니다.\n\n체크박스를 해제하고 평균 매수가를 직접 입력해주세요.';
			}
			if (error.response?.data?.message) {
				errorMessage += '\n\n상세: ' + error.response.data.message;
			}
			
			alert(errorMessage);
		} finally {
			setIsSubmitting(false);
		}
	};

	const resetForm = () => {
		setKeyword('');
		setSearchResults([]);
		setSelectedStock(null);
		setQuantity('');
		setAveragePrice('');
		setPurchaseDate('');
		setUseAutoPrice(false);
	};

	const handleClose = () => {
		resetForm();
		onClose();
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
				padding: '30px',
				borderRadius: '8px',
				width: '500px',
				maxHeight: '80vh',
				overflow: 'auto'
			}}>
				<h2>주식 추가</h2>

				{/* 종목 검색 */}
				<div style={{ marginBottom: '20px' }}>
					<label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
						종목 검색
					</label>
					<div style={{ display: 'flex', gap: '10px' }}>
						<input
							type="text"
							value={keyword}
							onChange={(e) => setKeyword(e.target.value)}
							onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
							placeholder="종목명 또는 종목코드 입력"
							style={{ flex: 1, padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
						/>
						<button
							onClick={handleSearch}
							disabled={isSearching}
							style={{
								padding: '8px 16px',
								backgroundColor: '#007bff',
								color: 'white',
								border: 'none',
								borderRadius: '4px',
								cursor: 'pointer'
							}}
						>
							{isSearching ? '검색 중...' : '검색'}
						</button>
					</div>
				</div>

				{/* 검색 결과 */}
				{searchResults.length > 0 && (
					<div style={{
						marginBottom: '20px',
						maxHeight: '200px',
						overflow: 'auto',
						border: '1px solid #ccc',
						borderRadius: '4px'
					}}>
						{searchResults.map((stock) => (
							<div
								key={stock.code}
								onClick={() => handleSelectStock(stock)}
								style={{
									padding: '10px',
									cursor: 'pointer',
									borderBottom: '1px solid #eee',
									backgroundColor: 'white'
								}}
								onMouseEnter={(e) => e.target.style.backgroundColor = '#f0f0f0'}
								onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
							>
								<div style={{ fontWeight: 'bold' }}>{stock.name}</div>
								<div style={{ fontSize: '12px', color: '#666' }}>
									{stock.code} | {stock.market} | {stock.sector}
								</div>
							</div>
						))}
					</div>
				)}

				{/* 선택된 종목 */}
				{selectedStock && (
					<div style={{
						marginBottom: '20px',
						padding: '10px',
						backgroundColor: '#e7f3ff',
						borderRadius: '4px'
					}}>
						<strong>선택된 종목:</strong> {selectedStock.name} ({selectedStock.code})
					</div>
				)}

				{/* 수량 입력 */}
				<div style={{ marginBottom: '20px' }}>
					<label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
						보유 수량
					</label>
					<input
						type="number"
						value={quantity}
						onChange={(e) => setQuantity(e.target.value)}
						placeholder="보유 수량 입력"
						min="1"
						style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
					/>
				</div>

				{/* 평균 매수가 입력 */}
				<div style={{ marginBottom: '20px' }}>
					<div style={{ marginBottom: '10px' }}>
						<label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
							<input
								type="checkbox"
								checked={useAutoPrice}
								onChange={(e) => {
									setUseAutoPrice(e.target.checked);
									if (e.target.checked) {
										setAveragePrice('');
									} else {
										setPurchaseDate('');
									}
								}}
								style={{ marginRight: '8px' }}
							/>
							<span style={{ fontWeight: 'bold' }}>매수 날짜로 자동 가격 조회</span>
						</label>
					</div>

					{useAutoPrice ? (
						<div>
							<label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
								매수 날짜
							</label>
							<input
								type="date"
								value={purchaseDate}
								onChange={(e) => setPurchaseDate(e.target.value)}
								style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
							/>
							<small style={{ color: '#666', display: 'block', marginTop: '5px' }}>
								* 선택한 날짜의 종가를 자동으로 조회합니다.
							</small>
							<small style={{ color: '#ff6b6b', display: 'block', marginTop: '3px', fontSize: '11px' }}>
								⚠️ API 연결 문제로 실패할 수 있습니다. 실패 시 수동 입력을 사용하세요.
							</small>
						</div>
					) : (
						<div>
							<label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
								평균 매수가 (원)
							</label>
							<input
								type="number"
								value={averagePrice}
								onChange={(e) => setAveragePrice(e.target.value)}
								placeholder="평균 매수가 입력"
								min="0"
								step="0.01"
								style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}
							/>
						</div>
					)}
				</div>

				{/* 버튼 */}
				<div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
					<button
						onClick={handleClose}
						style={{
							padding: '10px 20px',
							backgroundColor: '#6c757d',
							color: 'white',
							border: 'none',
							borderRadius: '4px',
							cursor: 'pointer'
						}}
					>
						취소
					</button>
					<button
						onClick={handleSubmit}
						disabled={isSubmitting}
						style={{
							padding: '10px 20px',
							backgroundColor: isSubmitting ? '#6c757d' : '#28a745',
							color: 'white',
							border: 'none',
							borderRadius: '4px',
							cursor: isSubmitting ? 'not-allowed' : 'pointer',
							opacity: isSubmitting ? 0.7 : 1
						}}
					>
						{isSubmitting ? '추가 중...' : '추가'}
					</button>
				</div>
			</div>
		</div>
	);
}

export default AddStockModal;
