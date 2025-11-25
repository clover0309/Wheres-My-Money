import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authAPI } from "../api/authAPI";
import AddStockModal from "../components/AddStockModal";
import { useAuth } from "../contexts/AuthContext";

function UserStockPage() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [apiStatus, setApiStatus] = useState('loading'); // 'loading', 'success', 'error'
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [stocks, setStocks] = useState([]);
  const [isLoadingStocks, setIsLoadingStocks] = useState(false);

    useEffect(() => {
        const testConnection = async () => {
            try {
                const response = await axios.post('http://localhost:8080/api/stock/test-connection', {}, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.data.success) {
                    setApiStatus('success');
                } else {
                    setApiStatus('error');
                }
            } catch (error) {
                console.error('API 연결 오류:', error);
                setApiStatus('error');
            }
        };

        testConnection();
    }, []);

    useEffect(() => {
        if (user) {
            loadStocks();
        }
    }, [user]);

    const loadStocks = async () => {
        setIsLoadingStocks(true);
        try {
            const response = await axios.get(`http://localhost:8080/api/stock/list?userId=${user}`);
            setStocks(response.data);
        } catch (error) {
            console.error('주식 목록 조회 오류:', error);
        } finally {
            setIsLoadingStocks(false);
        }
    };

    const handleLogout = async () => {
        try {
            const response = await authAPI.logout();
            
            if (response.success) {
                logout();
                alert('로그아웃되었습니다.');
                navigate("/");
            } else {
                alert(response.message || '로그아웃에 실패했습니다.');
            }
        } catch (error) {
            console.error('로그아웃 오류:', error);
            // 에러가 발생해도 로컬 상태는 초기화
            logout();
            navigate("/");
        }
    };

    const getApiStatusText = () => {
        switch(apiStatus) {
            case 'loading':
                return <span style={{ color: 'gray' }}>키움증권 API 호출중...</span>;
            case 'success':
                return <span style={{ color: 'green' }}>키움증권 API 연결 성공!</span>;
            case 'error':
                return <span style={{ color: 'red' }}>키움증권 API 연결 실패.</span>;
            default:
                return null;
        }
    };

    const handleStockAdded = () => {
        // 주식 추가 후 목록 새로고침
        loadStocks();
    };

    const handleDeleteStock = async (stockIdx, stockName) => {
        if (!window.confirm(`'${stockName}' 종목을 삭제하시겠습니까?`)) {
            return;
        }

        try {
            const response = await axios.delete(
                `http://localhost:8080/api/stock/delete?stockIdx=${stockIdx}&userId=${user}`
            );

            if (response.data.success) {
                alert('주식이 삭제되었습니다.');
                loadStocks();
            } else {
                alert(response.data.message || '주식 삭제에 실패했습니다.');
            }
        } catch (error) {
            console.error('주식 삭제 오류:', error);
            alert('주식 삭제 중 오류가 발생했습니다.');
        }
    };

    return (
        <div>
            <h1>로그인 성공</h1>
            {user && <p>환영합니다, {user}님!</p>}
            <button onClick={handleLogout}>로그아웃</button>

            <hr />

            <div style={{ marginTop: '20px' }}>
                <button 
                    onClick={() => setIsModalOpen(true)}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '16px',
                        fontWeight: 'bold'
                    }}
                >
                    주식 추가
                </button>
            </div>

            <hr />

            {/* 주식 목록 */}
            <div style={{ marginTop: '30px' }}>
                <h2>보유 주식 목록</h2>
                {isLoadingStocks ? (
                    <p>로딩 중...</p>
                ) : stocks.length === 0 ? (
                    <p style={{ color: '#666' }}>보유 중인 주식이 없습니다.</p>
                ) : (
                    <table style={{ 
                        width: '100%', 
                        borderCollapse: 'collapse',
                        marginTop: '20px'
                    }}>
                        <thead>
                            <tr style={{ backgroundColor: '#f8f9fa' }}>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'left' }}>종목코드</th>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'left' }}>종목명</th>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>보유수량</th>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>평균매수가</th>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>평가금액</th>
                                <th style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'center' }}>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            {stocks.map((stock) => (
                                <tr key={stock.userstockIdx}>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6' }}>{stock.userstockStk}</td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6' }}>{stock.userstockName}</td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>
                                        {stock.userstockQuantity.toLocaleString()}주
                                    </td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>
                                        {Number(stock.userstockAvgprice).toLocaleString()}원
                                    </td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'right' }}>
                                        {(Number(stock.userstockAvgprice) * stock.userstockQuantity).toLocaleString()}원
                                    </td>
                                    <td style={{ padding: '12px', border: '1px solid #dee2e6', textAlign: 'center' }}>
                                        <button
                                            onClick={() => handleDeleteStock(stock.userstockIdx, stock.userstockName)}
                                            style={{
                                                padding: '6px 12px',
                                                backgroundColor: '#dc3545',
                                                color: 'white',
                                                border: 'none',
                                                borderRadius: '4px',
                                                cursor: 'pointer',
                                                fontSize: '14px'
                                            }}
                                            onMouseOver={(e) => e.target.style.backgroundColor = '#c82333'}
                                            onMouseOut={(e) => e.target.style.backgroundColor = '#dc3545'}
                                        >
                                            삭제
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <AddStockModal 
                isOpen={isModalOpen} 
                onClose={() => setIsModalOpen(false)}
                onSuccess={handleStockAdded}
            />

            <div style={{ 
                position: 'fixed', 
                bottom: '20px', 
                left: '20px', 
                fontSize: '14px',
                fontWeight: 'bold'
            }}>
                {getApiStatusText()}
            </div>
        </div>
    );
}

export default UserStockPage;