using System;
using ProjectRelay.Gameplay;
using UnityEngine;

namespace ProjectRelay.Player
{
    [RequireComponent(typeof(CharacterController))]
    public class PlayerLocomotion : MonoBehaviour
    {
        [Header("Base Movement")]
        [SerializeField] private float baseMoveSpeed = 4.5f;
        [SerializeField] private float rotationSpeed = 10f;
        [SerializeField] private float gravity = -20f;

        [Header("Stamina")]
        [SerializeField] private float maxStamina = 100f;
        [SerializeField] private float staminaDrainPerSecond = 12f;
        [SerializeField] private float staminaRecoverPerSecond = 10f;

        [Header("Slip")]
        [SerializeField] private float slipStunSeconds = 0.6f;
        [SerializeField] private float slipCargoDamage = 5f;

        private CharacterController _controller;
        private LoadoutWeightSystem _weightSystem;
        private PlayerTerrainState _terrainState;
        private MobileInputBridge _mobileInput;
        private BalanceAssistController _balanceAssist;

        private float _verticalVelocity;
        private float _currentStamina;
        private float _stunTimer;

        public event Action Slipped;

        public float CurrentStamina => _currentStamina;
        public float StaminaNormalized => maxStamina <= 0 ? 0 : _currentStamina / maxStamina;

        private void Awake()
        {
            _controller = GetComponent<CharacterController>();
            _weightSystem = GetComponent<LoadoutWeightSystem>();
            _terrainState = GetComponent<PlayerTerrainState>();
            _mobileInput = GetComponent<MobileInputBridge>();
            _balanceAssist = GetComponent<BalanceAssistController>();
            _currentStamina = maxStamina;
        }

        private void Update()
        {
            TickSlipTimer();
            TickMovement();
            TickStamina();
            TrySlip();
        }

        private Vector2 ReadMoveInput()
        {
            return _mobileInput != null
                ? _mobileInput.MoveInput
                : new Vector2(Input.GetAxisRaw("Horizontal"), Input.GetAxisRaw("Vertical"));
        }

        private void TickMovement()
        {
            Vector2 move2D = ReadMoveInput();
            Vector3 input = new Vector3(move2D.x, 0f, move2D.y);
            input = Vector3.ClampMagnitude(input, 1f);

            if (_stunTimer > 0f)
                input = Vector3.zero;

            if (input.sqrMagnitude > 0.01f)
            {
                Quaternion targetRotation = Quaternion.LookRotation(input, Vector3.up);
                transform.rotation = Quaternion.Slerp(transform.rotation, targetRotation, rotationSpeed * Time.deltaTime);
            }

            float staminaMul = 0.65f + (0.35f * StaminaNormalized);
            float weightMul = _weightSystem != null ? _weightSystem.GetMoveSpeedMultiplier() : 1f;
            Vector3 move = input * (baseMoveSpeed * staminaMul * weightMul);

            if (_controller.isGrounded && _verticalVelocity < 0f)
                _verticalVelocity = -2f;

            _verticalVelocity += gravity * Time.deltaTime;
            move.y = _verticalVelocity;

            _controller.Move(move * Time.deltaTime);
        }

        private void TickStamina()
        {
            Vector2 move2D = ReadMoveInput();
            bool isMoving = move2D.sqrMagnitude > 0.01f;
            float terrainMul = _terrainState != null ? _terrainState.StaminaDrainMultiplier : 1f;

            float assistStaminaMul = _balanceAssist != null ? _balanceAssist.GetStaminaDrainMultiplier() : 1f;

            if (isMoving && _stunTimer <= 0f)
                _currentStamina -= staminaDrainPerSecond * terrainMul * assistStaminaMul * Time.deltaTime;
            else
                _currentStamina += staminaRecoverPerSecond * Time.deltaTime;

            _currentStamina = Mathf.Clamp(_currentStamina, 0f, maxStamina);
        }

        private void TrySlip()
        {
            if (_stunTimer > 0f) return;

            Vector2 move2D = ReadMoveInput();
            bool isMoving = move2D.sqrMagnitude > 0.01f;
            if (!isMoving) return;

            float baseChance = _weightSystem != null ? _weightSystem.GetFallChancePerSecond() : 0.01f;
            float bonus = _terrainState != null ? _terrainState.SlipChanceBonus : 0f;
            float assistMul = _balanceAssist != null ? _balanceAssist.GetSlipChanceMultiplier() : 1f;
            float chanceThisFrame = (baseChance + bonus) * assistMul * Time.deltaTime;

            if (UnityEngine.Random.value < chanceThisFrame)
                TriggerSlip();
        }

        private void TriggerSlip()
        {
            _stunTimer = slipStunSeconds;
            Debug.Log("[Player] Slipped!");
            Slipped?.Invoke();

            var mission = GetComponent<DeliveryMissionController>();
            if (mission != null)
                mission.DamageCargo(slipCargoDamage);
        }

        private void TickSlipTimer()
        {
            if (_stunTimer <= 0f) return;
            _stunTimer -= Time.deltaTime;
        }
    }
}
