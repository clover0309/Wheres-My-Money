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
	const [isSearching, setIsSearching] = useState(false);

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
		if (!selectedStock) {
			alert('종목을 선택해주세요.');
			return;
		}

		if (!quantity || parseInt(quantity) <= 0) {
			alert('수량을 입력해주세요.');
			return;
		}

		if (!averagePrice || parseFloat(averagePrice) <= 0) {
			alert('평균 매수가를 입력해주세요.');
			return;
		}

		try {
			const response = await axios.post('http://localhost:8080/api/stock/add', {
				userId: user,
				stockCode: selectedStock.code,
				stockName: selectedStock.name,
				quantity: parseInt(quantity),
				averagePrice: parseFloat(averagePrice)
			}, {
				headers: {
					'Content-Type': 'application/json'
				}
			});

			if (response.data.success) {
				alert('주식이 추가되었습니다.');
				resetForm();
				onSuccess();
				onClose();
			} else {
				alert(response.data.message || '주식 추가에 실패했습니다.');
			}
		} catch (error) {
			console.error('주식 추가 오류:', error);
			alert('주식 추가에 실패했습니다.');
		}
	};

	const resetForm = () => {
		setKeyword('');
		setSearchResults([]);
		setSelectedStock(null);
		setQuantity('');
		setAveragePrice('');
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
						style={{
							padding: '10px 20px',
							backgroundColor: '#28a745',
							color: 'white',
							border: 'none',
							borderRadius: '4px',
							cursor: 'pointer'
						}}
					>
						추가
					</button>
				</div>
			</div>
		</div>
	);
}

export default AddStockModal;
