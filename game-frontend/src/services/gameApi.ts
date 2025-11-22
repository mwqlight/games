import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
});

export interface Move {
  fromX: number;
  fromY: number;
  toX: number;
  toY: number;
}

export interface ChessPiece {
  id: string;
  type: string;
  color: string;
  position: string;
  captured: boolean;
}

export interface GameState {
  board: ChessPiece[];
  currentPlayer: string;
  gameStatus: string;
  winner: string | null;
}

export const startGame = async (): Promise<GameState> => {
  const response = await api.post('/game/start');
  return response.data;
};

export const getGameState = async (): Promise<GameState> => {
  const response = await api.get('/game/state');
  return response.data;
};

export const makeMove = async (move: Move): Promise<GameState> => {
  const response = await api.post('/game/move', move);
  return response.data;
};